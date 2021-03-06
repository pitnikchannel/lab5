package main.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.reader.ConsoleReader;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

public class Person implements Comparable<Person> {
	private Integer id;
	private String name;
	private Coordinates coordinates;
	private java.time.LocalDateTime creationDate;
	private double height;
	private String passportID;
	private Color hairColor;
	private Country nationality;
	private Location location;
	private static Gson gson = new GsonBuilder()
			//.serializeNulls()
			.create();

	/**
	 * Считвыает Person из json строки.
	 *
	 * @param json Объект в json формате
	 * @return Person
	 */
	public static Person parsePerson(String json) throws WrongPersonException {
		Person person = null;
		try {
			String finding = ",\"y\":";
//			System.out.println(finding);
//			System.out.println(json);
			person = gson.fromJson(json, Person.class);
			person.validate();
			int occurrencesCount = json.length() - json.replace(finding, "").length();
			if ((json.contains((char) 34+"y"+(char) 34+":null")) || occurrencesCount !=10)  throw new WrongPersonException("coordinates(Y не может быть null/Y должен существовать)");
		} catch (WrongPersonException e){
			e.getMessage();
			System.exit(1);
		}
		return person;
	}

	/**
	 * Переводит объект в json строку
	 *
	 * @return String json
	 */
	public String toJSON() {
		return gson.toJson(this);
	}

	public long getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Coordinates getCoords() {
		return coordinates;
	}

	public Location getLocation() {
		return location;
	}

	/**
	 * Проверка JSON файла на соотвествие параметров Person всем условиям
	 *
	 * @throws WrongPersonException если Person создан некорректно
	 * @throws WrongDateTimeException если дата/время не являются корректными
	 */
	public void validate() throws WrongPersonException {
	try {
		if (this.id <= 0) throw new WrongPersonException("id");
		if (this.name == null || this.name.equals("")) throw new WrongPersonException("name");
		if (this.coordinates == null) throw new WrongPersonException("coordinates");
		if (this.creationDate == null) throw new WrongPersonException("creationDate");
		if (this.height <= 0) throw new WrongPersonException("height");
		if (this.hairColor == null) throw new WrongPersonException("hairColor");
		if (this.location == null) throw new WrongPersonException("location");
		if (this.coordinates.getX() == null) throw new WrongPersonException("coordinates (X)");
		if ((this.passportID != null && this.passportID.length() < 4)) throw new WrongPersonException("passportID");
		else if (!this.getCreationDate().equals(creationDate)) throw new WrongDateTimeException();
	} catch (WrongPersonException e){
		e.getMessage();
		System.exit(0);
	}
	}


	public LocalDateTime getCreationDate() {
		if (creationDate == null) throw new WrongDateTimeException();
		else {

			try {
				LocalDate.parse(creationDate.toLocalDate().toString());
				LocalTime.parse(creationDate.toLocalTime().toString());
				return creationDate;
			} catch (DateTimeException e) {
				throw new WrongDateTimeException();
			}
		}
	}

	/**
	 * Заполняет Person данными полученными с консоли
	 *
	 * @param scanner
	 * @return Person
	 */
	public static Person fillPerson(Scanner scanner) {
		Person person = new Person();
		try {
			System.out.println("Ввод объекта Person:");
			person.name = (String) ConsoleReader.conditionalRead(scanner, "Введите имя: ",false,
					String::toString, Objects::nonNull, (m) -> !m.equals(""));
			person.coordinates = Coordinates.fillCoordinates(scanner);
			person.creationDate = LocalDateTime.now();
			person.height = (double) ConsoleReader.conditionalRead(scanner, "Введите рост: ",false,
					Double::parseDouble, (m) -> Double.parseDouble(m) > 0);
			person.passportID = (String) ConsoleReader.conditionalRead(scanner, "Введите индетификатор паспорта: ", true,
					String::toString, (m) -> m.length() > 3, (m) -> !m.equals(""));
			person.hairColor = Color.fillColor(scanner);
			person.nationality = Country.fillCountry(scanner);
			person.location = Location.fillLocation(scanner);
			person.id = IdGenerator.generateUniqueId();
		} catch (NoSuchElementException e) {
			System.err.println("Ну почему-ты до конца не ввел Person :(");
			System.exit(0);
		}
		return person;
	}


	/**
	 * Заполняет Person полученными данными из файла
	 *
	 * @param scanner
	 * @return Person
	 * @throws IOException В случае ошибок файла
	 */
	public static Person fillPersonFromFile(Scanner scanner) throws IOException {
		Person person = new Person();
			person.name = (String) ConsoleReader.conditionalRead(scanner,"",false,
					String::toString, Objects::nonNull, (m) -> !m.equals(""));
			person.coordinates = Coordinates.fillCoordinatesFromFile(scanner);
			person.creationDate = LocalDateTime.now();
			person.height = (double) ConsoleReader.conditionalRead(scanner,"",false,
					Double::parseDouble, (m) -> Double.parseDouble(m) > 0);
			person.passportID = (String) ConsoleReader.conditionalRead(scanner,"",true,
					String::toString, (m) -> m.length() > 3, (m) -> !m.equals(""));
			person.hairColor = Color.fillColorFromFile(scanner);
			person.nationality = Country.fillCountryFromFile(scanner);
			person.location = Location.fillLocationFromFile(scanner);
			person.id = IdGenerator.generateUniqueId();
			person.validate();
			System.out.println("Person "+ person.name + " успешно создан");
		return person;
	}

	public static class IdGenerator {
		public static int generateUniqueId() {
			UUID idOne = UUID.randomUUID();
			String str = "" + idOne;
			int uid = str.hashCode();
			String filterStr = "" + uid;
			str = filterStr.replaceAll("-", "");
			return Integer.parseInt(str);
		}
	}

	@Override
	public String toString() {
		return toJSON();
	}

	@Override
	public int hashCode() {
			int result = 17;

			result = 31 * result + name.hashCode();
			result = 31 * result +
					(location == null
							? 0
							: location.hashCode());
			result = 31 * result +
					(coordinates == null
							? 0
							: coordinates.hashCode());
			result = 31 * result +
					(hairColor == null
							? 0
							: hairColor.hashCode());
			result = 31 * result +
					(nationality == null
							? 0
							: nationality.hashCode());
			result = 31 * result +
					(passportID == null
							? 0
							: passportID.hashCode());
			return result;
		}


	@Override
	public int compareTo(Person o) {
		if (id>o.getId()) return 1;
		else if (id<o.getId()) return -1;
		else return 0;
	}
}
