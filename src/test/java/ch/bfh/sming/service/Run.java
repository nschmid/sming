package ch.bfh.sming.service;

import java.util.Scanner;

public class Run {

	private Scanner scanner = new Scanner(System.in);

	public static void main(String args[]) {
		new Run();
	}

	public Run() {

		Txw51Service service = Txw51ServiceImpl.getInstance();
		// add name of serial port here:
		// service.initialize("/dev/tty.usbmodem1");
		service.initialize(null);

		service.listServices();
		selectService(service);
	}

	private void selectService(Txw51Service service) {
		System.out.println("\nSelect Service:");

		String handle = scanner.nextLine();
		service.readAttribute(Integer.parseInt(handle));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		selectService(service);
	}
}
