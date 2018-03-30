package com.mif.juje0729.ftp;

import android.os.Environment;
import android.text.util.Linkify;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPFunctions extends Thread {

    private String adress; // FTP serverio adresas
    private TextView text;
    private NotesDatabase db;
    private FTPClient ftp;
    private boolean download;
    private LauncherActivity activity;
    private int filesFound = 0;
    private double filesSize = 0;

    /*
    text - išvesties langas vartotojo grafinėje sąsajoje.
    db - failų sąrašą ir serverių sąrašą sauganti duomenų bazė.
    ftp - FTP klientas, komunikuojantis su FTP serveriu.
    download - jei true, tai vykdomas atsisiuntimas, jei false, tai vykdoma paieška.
    activity - Vartotojo grafinės sąsajos pagrindinis (matomas atidarius programą) langas.
     */
    FTPFunctions(TextView tv, NotesDatabase ndb, FTPClient ftpc, boolean isDownload,
                 LauncherActivity la) {
        text = tv;
        db = ndb;
        ftp = ftpc;
        download = isDownload;
        activity = la;
    }

    /*
    Apdoroja serverio įvestį. Serverio įvestis yra pavidalo:
    ServerioAdresas,vartotojoPrisijungimoVardas,Slaptažodis
    Taigi pagal skirtuką (kablelį) adresas, vardas ir slaptažodis atskiriami ir išsaugomi masyve
    */
    private String[] parseServerInput(String input) {
        /*
        replaceAll(" ", "") pašalina tarpus
        replaceAll(",,", ",\0,") įterpia null simbolį tarp dviejų iš eilės einančių kablelių, kad
        būtų korektiškai atskirta įvestis. Plačiau:
        Įvestyje du iš eilės einantys kableliai reiškia, kad vardo ir slaptažodžio vartotojas
        nenurodė - tikriausiai dėl to, kad serveris jų nereikalauja (yra viešas). Tokia įvestis
        atrodo:
        ServerioAdresas,,
        Apdorojant įvestį, dviejų iš eilės einančių kablelių palikti negalima. Reikia tarp jų bent
        vieno simbolio, pagal susitarimą reiškiančio tuščią vietą (nenurodytą vartotojo prisijungimo
        vardą). Kitu atveju, įvestį atskirs nekorektiškai. Įvestį atskirs nekorektiškai ir tuo
        atveju, jei nenurodytas vartotojo vardas ir slaptažodis, ir įvesties gale nėra nei vieno
        simbolio, pagal susitarimą reiškiančio tuščią vietą (nenurodytą slaptažodį). Todėl,
        apsidraudimui, prieš paduodant įvestį kaip argumentą šiam metodui, įvesties gale
        prilipinamas sutartas simbolis. Šioje programoje sutartas simbolis tuščiai vietai (
        nenurodytam vartotojo vardui ir slaptažodžiui) žymėti yra null ("\0")
         */
        String[] serverInfo = input.replaceAll(" ", "").replaceAll(",,", ",\0,").split(",");
        String _adress = serverInfo[0];
        String _directory = "/"; // Pradinė (šakninė) serverio direktorija yra /
        /*
        Atranda, nuo kurio indekso adrese prasideda šakninės direktorijos vidinė(s) direktorija(-os)
         */
        int childDirectory = _adress.indexOf("/");
        /*
        Jei vidinė(s) direktorija(-os) egzistuoja, tai jos(-ų) pavadinimą priskiriam kintamajam
        _directory. Pvz., jei įvestyje nurodyta serverio direktorija yra ftp.cs.umd.edu/pub
        tai _directory įgaus reikšmę pub
        Jei įvestyje nurodyta ftp.cs.umd.edu/pub/amanda, tai _directory įgaus reikšmę pub/amanda
        _adress kintamajam priskiriame serverio adresą be jokių direktorijų. Pagal aukštesnius
        pavyzdžius, _adress įgaus reikšmę ftp.cs.umd.edu
         */
        if (childDirectory != -1) {
            _directory = _adress.substring(childDirectory + 1);
            _adress = _adress.substring(0, childDirectory);
        }
        String _username = serverInfo[1];
        /*
        Jei vartotojo vardas nenurodytas, pagal FTP taisykles duodame jam anoniminio prisijungimo
        vardą - anonymous
         */
        if (_username.equals("\0")) {
            _username = "anonymous";
        }
        String _password = serverInfo[2];
        /*
        Jei slaptažodis nenurodytas, pagal FTP taisykles duodame jam anoniminio prisijungimo
        slaptažodį - anonymous@domain.com
        Čia turėtų tikti bet koks elektroninio pašto adresas
         */
        if (_password.equals("\0")) {
            _password = "anonymous@domain.com";
        }
        /*
        Slaptažodis yra paskutinis žodis serverio įvestyje, jo gale būna priklijuojamas null
        simbolis, kurį dabar reikia pašalinti
         */
        _password = _password.replaceAll("\0", "");
        return new String[]{_adress, _username, _password, _directory};
    }

    /*
    Įrašo tekstą į išvesties langą.
    txt - įrašomas tekstas.
     */
    public void addText(final String txt) {
        /*
        Valdyti grafinę vartotojo sąsają (šiuo atveju atvaizduoti naują išvestį) gali tik ją
        sukūrusi gija. Sisteminis metodas runOnUiThread perduoda tai gijai naujos išvesties
        atvaizdavimo užduotį
         */
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.append(txt);
                /*
                Vartotojo grafinėje sąsajoje paryškina nuorodas ir padaro spaudžiamas (vartotojui
                paspaudus ant nuorodos, ji atidaroma su numatytąja interneto naršykle)
                 */
                Linkify.addLinks(text, Linkify.WEB_URLS);
                /*
                Kai į vartotojo sąsają įrašoma nauja išvestis, tikrinama, ar visa išvestis
                matoma. Jei ne, tai paslenkama žemyn, kad naujausia išvestis būtų matoma.
                 */
                text.post(new Runnable() {
                    @Override
                    public void run() {
                        final int scrollAmount = text.getLayout().getLineTop(text.getLineCount())
                                - text.getHeight();
                        if (scrollAmount > 0)
                            text.scrollTo(0, scrollAmount);
                        else
                            text.scrollTo(0, 0);
                    }
                });
            }
        });
    }

    @Override
    public void run() {

        if (download) {
            /*
            Failai atsiunčiami į Download aplanką
             */
            autoDownload(ftp, db.getServers(), db.getFiles(), Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
            activity.downloadClicked = false;
        } else {
            search(ftp, db.getServers(), db.getFiles());
            activity.searchClicked = false;
        }
    }

    /*
    Prisijungia prie FTP serverio.
     */
    public boolean connect(FTPClient ftp, String server, String username,
                           String password) {
        try {
            ftp.connect(server);
            int r = ftp.getReplyCode();
            String[] reply = ftp.getReplyStrings();
            if (!FTPReply.isPositiveCompletion(r)) {
                addText(ftp.getReplyString() + "\n");
            } else {
                addText("Prisijungta prie " + server + "\n");
                addText("Bandoma autentifikuotis su username ir password..." + "\n");
                boolean connected = ftp.login(username, password);
                if (connected) {
                    addText("Pavyko. Atsakymas is serverio:" + "\n");
                    for (String line : reply) {
                        addText(line + "\n");
                    }
                    /*
                    Dažnokai, neperėjus į šią būsena, gali iškilti problemos su užklausomis
                    FTP serveriui
                     */
                    ftp.enterLocalPassiveMode();
                    adress = server;
                    return true;
                } else {
                    addText(ftp.getReplyString() + "\n");
                }
            }
            return false;
        } catch (Exception ex) {
            addText("Serveris adresu " + server + " nerastas." + "\n");
            addText(ex.toString() + "\n");
            return false;
        }
    }

    /*
    Iteruoja per serverių ir failų sąrašą, atrastus failus atsisiunčia
     */
    public void autoDownload(FTPClient ftp, List<Note> servers, List<Note> files, String
            downloadDirectory) {
        try {
            for (Note note : servers) {
                /*
                Įterpia null simbolį į serverio įvesties galą, kad metode parseServerInput būtų
                korektiškai atskirta serverio įvestis. Plačiau: 
                Apdorojant serverio įvestį, jei prisijungimo vardas ir slaptažodis nenurodyta (kas
                tikriausiai reiškia, kad serveris jų nereikalauja - yra viešas), tai įvestis vietoj
                ServerioAdresas,VartotojoVardas,Slaptažodis
                atrodys
                ServerioAdresas,,
                Matome, kad po antro skirtuko (kablelio) neegzistuoja joks simbolis. Todėl po antro
                kablelio, kaip susitarimą, kad slaptažodis neįvestas, įklijuojame null simbolį.
                Beje, tarp kablelių irgi nėra jokio simbolio, kuris, pagal susitarimą, žymėtų, kad
                vartotojo vardas neįvestas. Null simbolį tarp kablelių įterpia serverio įvesties
                apdorojimo metodas - parseServerInput. Plačiau - žr. minėto metodo antrą komentarą.
                 */
                String server = note.getNote() + "\0";
                String ServerInput[] = parseServerInput(server);
                String adress = ServerInput[0], username = ServerInput[1], password =
                        ServerInput[2], directory = ServerInput[3];
                if (connect(ftp, adress, username, password)) {
                    addText("Bandoma ieskoti failu " + adress + "...\n");
                    FTPFile[] filesInDirectory = ftp.listFiles(directory);
                    if (filesInDirectory != null && filesInDirectory.length > 0) {
                        for (Note note2 : files) {
                            String file = note2.getNote();
                            autoDownload(ftp, directory, file, downloadDirectory);
                        }
                        addText("Paieska " + adress + " baigta\n");
                    } else {
                        addText("Direktorija " + adress + " neegzistuoja arba tuscia\n");
                    }
                    addText("\n");
                }
            }
            if (filesFound > 0) {
                /*
                BigDecimal naudojamas skaičiaus suformatavimui, kad po kablelio būtų 5 skaitmenys
                 */
                addText("Atsisiuntimas baigtas." + "\n" + "Atsisiusta failu: " + filesFound +
                        "\n" + "Failai issaugoti aplanke Download." + "\n" + "Jie uzima " + new
                        BigDecimal(filesSize).setScale(5, BigDecimal.ROUND_HALF_UP) + " MB" + "\n");
            } else {
                addText("Nieko neatsiunte. Bandykite pakeisti serveriu ir (arba) failu sarasa." +
                        "\n");
            }
            filesFound = 0;
            filesSize = 0;
        } catch (Exception ex) {
            if (ex.getClass() == ArrayIndexOutOfBoundsException.class) {
                addText(ex.toString() + "\n" + "Serveriu sarase tikriausiai yra klaida." + "\n");
            } else {
                addText(ex.toString() + "\n");
            }
        }
    }

    /*
    Rekursiškai ieškomi ir atsisiunčiami failai
     */
    public void autoDownload(FTPClient ftp, String directory,
                             String key, String downloadDirectory) {
        try {
            FTPFile[] files = ftp.listFiles(directory);
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    String fileName = file.getName();
                    if (fileName.equals(".")
                            || fileName.equals("..")) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        autoDownload(ftp, directory + "/" + fileName, key, downloadDirectory);
                    } else {
                        if (fileName.contains(key)) {
                            download(ftp, directory + "/" + fileName, fileName,
                                    downloadDirectory, Double.parseDouble(String.valueOf(file
                                            .getSize())) / 1048576);
                        }
                    }
                }
            }
        } catch (Exception e) {
            addText(ftp.getReplyString() + "\n");
            e.printStackTrace();
        }
    }

    /*
    Atrasto failo atsisiuntimas
     */
    public void download(FTPClient ftp, String Directory, String file,
                         String downloadDirectory, double fileSize) {
        int r = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(r)) {
            addText(ftp.getReplyString() + "\n");
            return;
        }
        try {
            OutputStream o;
            o = new FileOutputStream(new File(downloadDirectory, file));
            boolean atsiusta = ftp.retrieveFile(Directory, o);

            if (atsiusta) {
                filesFound++;
                filesSize += fileSize;
                addText("Atsiunte " + adress + "/" + Directory + "\n");
            } else {
                addText(ftp.getReplyString() + "\n");
            }
            o.close();
        } catch (Exception ex) {
            addText(ftp.getReplyString() + "\n");
            addText(ex.toString() + "\n");
        }
    }

    /*
    Iteruoja per serverių ir failų sąrašą, atrastus failus išveda į ekraną
     */
    public void search(FTPClient ftp, List<Note> servers, List<Note> files) {
        try {
            for (Note note : servers) {
                String server = note.getNote() + "\0";
                String ServerInput[] = parseServerInput(server);
                String adress = ServerInput[0], username = ServerInput[1], password =
                        ServerInput[2], directory = ServerInput[3];
                if (connect(ftp, adress, username, password)) {
                    addText("Bandoma ieskoti failu " + adress + "...\n");
                    FTPFile[] filesInDirectory = ftp.listFiles(directory);
                    if (filesInDirectory != null && filesInDirectory.length > 0) {
                        for (Note note2 : files) {
                            String file = note2.getNote();
                            search(ftp, directory, file);
                        }
                        addText("Paieska " + adress + " baigta\n");
                    } else {
                        addText("Direktorija " + adress + " neegzistuoja arba tuscia\n");
                    }
                    addText("\n");
                }
            }
            if (filesFound > 0) {
                addText("Paieska baigta." + "\n" + "Atrasta failu: " + filesFound + "\n" + "Jie " +
                        "uzima " + new BigDecimal(filesSize).setScale(5, BigDecimal
                        .ROUND_HALF_UP) + " MB" + "\n");
            } else {
                addText("Paieska baigta. Nieko neatrado. Bandykite pakeisti serveriu ir (arba) " +
                        "failu sarasa." + "\n");
            }
            filesFound = 0;
            filesSize = 0;
        } catch (Exception ex) {
            if (ex.getClass() == ArrayIndexOutOfBoundsException.class) {
                addText(ex.toString() + "\n" + "Serveriu sarase tikriausiai yra klaida." + "\n");
            } else {
                addText(ex.toString() + "\n");
            }
        }
    }

    /*
    Rekursiškai ieškomi failai, atrastų failų pavadinimai išvedami į ekraną
     */
    public void search(FTPClient ftp, String directory,
                       String key) {
        try {
            FTPFile[] files = ftp.listFiles(directory);
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    String fileName = file.getName();
                    if (fileName.equals(".")
                            || fileName.equals("..")) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        search(ftp, directory + "/" + fileName, key);
                    } else {
                        if (fileName.contains(key)) {
                            filesFound++;
                            double fileSize = Double.parseDouble(String.valueOf(file.getSize()))
                                    / 1048576;
                            filesSize += fileSize;
                            /*
                            SimpleDateFormat suformatuoja skaitomą failo datą vartotojui
                             */
                            addText("Atrado " + adress + "/" + directory + "/" + fileName + " ("
                                    + new SimpleDateFormat("yyyy-MM-dd").format(file.getTimestamp
                                    ().getTime()) + ", " + new BigDecimal(fileSize).setScale(5,
                                    BigDecimal.ROUND_HALF_UP) + " MB)\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            addText(ftp.getReplyString() + "\n");
        }
    }

}
