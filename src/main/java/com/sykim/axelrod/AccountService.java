package com.sykim.axelrod;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.exceptions.AccountDoseNotExistException;
import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.model.Bank;
import com.sykim.axelrod.repository.AccountRepository;
import com.sykim.axelrod.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BankRepository bankRepository;

    public List<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Account changeAccountBalance(String accountNum, Double change) throws AccountDoseNotExistException {
        Optional<Account> accountOrNUll = accountRepository.findByAccountNum(accountNum);

        if (accountOrNUll.isEmpty()) throw new AccountDoseNotExistException("Account By account num : " + accountNum + " Not Found!");

        Account account = accountOrNUll.get();
        account.changeBalance(change);

        return accountRepository.save(account);
    }

    public List<Bank> getBankListFromCSV() throws IOException, CsvValidationException {
        List<Bank> bankList = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                Objects.requireNonNull(Objects.requireNonNull(classLoader.getResource("data/codefilex.csv")).getFile())
        );
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);

        String[] header = csvReader.readNext();
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerMap.put(checkBOM(header[i]), i);
        }

        String[] record;
        while((record = csvReader.readNext()) != null) {
            bankList.add(new Bank(
                    null,
                    checkBOM(record[headerMap.get("은행명")]) + checkBOM(record[headerMap.get("점포명")]),
                    checkBOM(record[headerMap.get("은행코드")]),
                    checkBOM(record[headerMap.get("주소")]),
                    checkBOM(record[headerMap.get("전화번호")]
                    )));
        }

        return bankList;
    }

    public void creteBankByList(List<Bank> bankList) {
        bankRepository.saveAll(bankList);
    }

    private String checkBOM(String input) {
        if (input.startsWith("\uFEFF")) return input.substring(1);
        else return input;
    }
}
