package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewok;
import bgu.spl.mics.application.passiveObjects.Input;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	private static Input input;
	private static Gson gson;
	public static CountDownLatch start=new CountDownLatch(4);

	public static void main(String[] args) {
		gson = new Gson();
		if (args.length != 2) {
				return;
			}
		try {
			Reader reader = new FileReader(args[0]);
			input = gson.fromJson(reader, Input.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Ewoks ewoks = Ewoks.getInstance();
		Diary diary = Diary.getInstance();
		ewoks.createSource(input.getEwoks());
		Thread Leia = new Thread(new LeiaMicroservice(input.getAttacks()));
		Thread HanSolo = new Thread(new HanSoloMicroservice());
		Thread C3PO = new Thread(new C3POMicroservice());
		Thread R2D2 = new Thread(new R2D2Microservice(input.getR2D2()));
		Thread Lando = new Thread(new LandoMicroservice(input.getLando()));
		Leia.start();
		HanSolo.start();
		C3PO.start();
		R2D2.start();
		Lando.start();
		try {
			Leia.join();
			HanSolo.join();
			C3PO.join();
			R2D2.join();
			Lando.join();
		} catch (InterruptedException e) { }
		try {
			gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter writer = new FileWriter(args[1]);
			gson.toJson(diary, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		start=new CountDownLatch(4);
	}
}


