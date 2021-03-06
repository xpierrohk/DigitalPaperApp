package net.sony.dpt;

import net.sony.dpt.error.SonyException;
import net.sony.dpt.persistence.DeviceInfoStore;
import net.sony.dpt.persistence.LastCommandRunStore;
import net.sony.dpt.persistence.RegistrationTokenStore;
import net.sony.dpt.persistence.SyncStore;
import net.sony.dpt.ui.cli.Command;
import net.sony.dpt.ui.cli.DigitalPaperCLI;
import net.sony.util.CryptographyUtils;
import net.sony.util.DiffieHelman;

import java.nio.file.Path;
import java.util.Scanner;

public class DigitalPaperApp {

    public static void main(String[] args)  {
        Path homeFolder = Path.of(System.getProperty("user.home"));

        DigitalPaperCLI digitalPaperCLI = new DigitalPaperCLI(
                new DiffieHelman(),
                new CryptographyUtils(),
                (message) -> {
                   System.out.println(message);
                   System.out.flush();
                },
                () -> {
                    Scanner scanner = new Scanner(System.in);
                    return scanner.nextLine();
                },
                new RegistrationTokenStore(homeFolder),
                new SyncStore(homeFolder),
                new DeviceInfoStore(homeFolder),
                new LastCommandRunStore(homeFolder, System.out::println)
        );
        try {
            digitalPaperCLI.execute(args);
        } catch (SonyException sonyException) {
            System.err.println("A device exception occurred: " + sonyException.print());
            sonyException.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(Command.printHelp());
            System.out.flush();
        }
    }

}
