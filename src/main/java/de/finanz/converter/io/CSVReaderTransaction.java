package de.finanz.converter.io;

import de.finanz.converter.transaction.Transaction;

public class CSVReaderTransaction extends CSVReader<Transaction> {
    public CSVReaderTransaction() {
        super(Transaction.class);
    }
}
