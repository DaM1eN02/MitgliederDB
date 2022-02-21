package dbfileorga;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MitgliederDB implements Iterable<Record>
{
	
	protected DBBlock db[] = new DBBlock[8];
	
	
	public MitgliederDB(boolean ordered){
		this();
		insertMitgliederIntoDB(ordered);
		
	}
	public MitgliederDB(){
		initDB();
	}
	
	private void initDB() {
		for (int i = 0; i<db.length; ++i){
			db[i]= new DBBlock();
		}
		
	}
	private void insertMitgliederIntoDB(boolean ordered) {
		MitgliederTableAsArray mitglieder = new MitgliederTableAsArray();
		String mitgliederDatasets[];
		if (ordered){
			mitgliederDatasets = mitglieder.recordsOrdered;
		}else{
			mitgliederDatasets = mitglieder.records;
		}
		for (String currRecord : mitgliederDatasets ){
			appendRecord(new Record(currRecord));
		}	
	}

		
	protected int appendRecord(Record record){
		//search for block where the record should be appended
		int currBlock = getBlockNumOfRecord(getNumberOfRecords());
		int result = db[currBlock].insertRecordAtTheEnd(record);
		if (result != -1 ){ //insert was successful
			return result;
		}else if (currBlock < db.length) { // overflow => insert the record into the next block
			return db[currBlock+1].insertRecordAtTheEnd(record);
		}
		return -1;
	}
	

	@Override
	public String toString(){
		String result = new String();
		for (int i = 0; i< db.length ; ++i){
			result += "Block "+i+"\n";
			result += db[i].toString();
			result += "-------------------------------------------------------------------------------------\n";
		}
		return result;
	}
	
	/**
	 * Returns the number of Records in the Database
	 * @return number of records stored in the database
	 */
	public int getNumberOfRecords(){
		int result = 0;
		for (DBBlock currBlock: db){
			result += currBlock.getNumberOfRecords();
		}
		return result;
	}
	
	/**
	 * Returns the block number of the given record number 
	 * @param recNum the record number to search for
	 * @return the block number or -1 if record is not found
	 */
	public int getBlockNumOfRecord(int recNum){
		int recCounter = 0;
		for (int i = 0; i< db.length; ++i){
			if (recNum <= (recCounter+db[i].getNumberOfRecords())){
				return i ;
			}else{
				recCounter += db[i].getNumberOfRecords();
			}
		}
		return -1;
	}
		
	public DBBlock getBlock(int i){
		return db[i];
	}
	
	
	/**
	 * Returns the record matching the record number
	 * @param recNum the term to search for
	 * @return the record matching the search term
	 */
	public Record read(int recNum) {
		int count = 0;
		for (DBBlock dbblock : db) { // Iteration über alle Datenbank Blöcke
			for (Record record : dbblock) { // Iteration über alle Records in jedem Block
				++count;
				if (count == recNum) { // Suchen von dem Record mit der Nummer recNum
					return record;
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns the number of the first record that matches the search term
	 * @param searchTerm the term to search for
	 * @return the number of the record in the DB -1 if not found
	 */
	public int findPos(String searchTerm){
		int count = 0;
		for (DBBlock dbBlock : db) { // Iteration über alle Datenbank Blöcke
			for (Record record : dbBlock) { // Iteration über alle Records in jedem Block
				++count;
				if (record.toString().startsWith(searchTerm)) { // Suchen nach dem String
					return count;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Inserts the record into the file and returns the record number
	 * @param record to be inserted Record
	 * @return the record number of the inserted record
	 */
	public int insert(Record record){
		if (record != null) { // Überprüfen ob das Insert nicht NULL ist
			this.appendRecord(record); // Nutzen der Funktion appendRecord
			return findPos(record.toString());
		}
		return -1;
	}
	
	/**
	 * Deletes the record specified 
	 * @param numRecord number of the record to be deleted
	 */
	public void delete(int numRecord) {
		DBBlock insert = new DBBlock(); // Erstellen eines neuen Blockes
		DBBlock deletedBlock = db[getBlockNumOfRecord(numRecord)]; // Block in dem der Record gelöscht werden muss

		for (Record record : deletedBlock) {
			if (!record.toString().equals(read(numRecord).toString())) {
				insert.insertRecordAtTheEnd(record); // Neuen Block mit den alten Sachen füllen und das eine Record löschen
			}
		}
		db[getBlockNumOfRecord(numRecord)] = insert; // Den alten Block durch den neuen ersetzen
	}
	
	/**
	 * Replaces the record at the specified position with the given one.
	 * @param numRecord the position of the old record in the db
	 * @param modifiedRecord the new record
	 * 
	 */
	public void modify(int numRecord, Record modifiedRecord){
		DBBlock modify = new DBBlock(); // Erstellen eines neues Blockes
		DBBlock modifiedBlock = db[getBlockNumOfRecord(numRecord)]; // Block in dem der Record verändert werden soll

		for (Record record : modifiedBlock) {
			if (!record.toString().equals(read(numRecord).toString())) {
				modify.insertRecordAtTheEnd(record); // Alte Sachen in neuen Block einfügen
			} else {
				modify.insertRecordAtTheEnd(modifiedRecord); // Modifizierten Record in den Block einfügen
			}
		}
		db[getBlockNumOfRecord(numRecord)] = modify; // Den alten Block durch den neuen ersetzen
	}

	
	@Override
	public Iterator<Record> iterator() {
		return new DBIterator();
	}
 
	private class DBIterator implements Iterator<Record> {

		    private int currBlock = 0;
		    private Iterator<Record> currBlockIter= db[currBlock].iterator();
	 
	        public boolean hasNext() {
	            if (currBlockIter.hasNext()){
	                return true;
	            }else if (currBlock < db.length){ //continue search in the next block
	            	return db[currBlock+1].iterator().hasNext();
	            }else{ 
	                return false;
	            }
	        }
	 
	        public Record next() {	        	
	        	if (currBlockIter.hasNext()){
	        		return currBlockIter.next();
	        	}else if (currBlock < db.length){ //continue search in the next block
	        		currBlockIter= db[++currBlock].iterator();
	        		return currBlockIter.next();
	        	}else{
	        		throw new NoSuchElementException();
	        	}
	        }
	 
	        @Override
	        public void remove() {
	        	throw new UnsupportedOperationException();
	        }
	    } 
	 

}
