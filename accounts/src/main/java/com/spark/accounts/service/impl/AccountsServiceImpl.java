package com.spark.accounts.service.impl;

import com.spark.accounts.constants.AccountsConstants;
import com.spark.accounts.dto.CustomerDto;
import com.spark.accounts.entity.Accounts;
import com.spark.accounts.entity.Customer;
import com.spark.accounts.exception.CustomerAlreadyExistsException;
import com.spark.accounts.mapper.CustomerMapper;
import com.spark.accounts.repository.AccountsRepository;
import com.spark.accounts.repository.CustomerRespository;
import com.spark.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.hibernate.cache.spi.support.CacheUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private AccountsRepository accountsRepository;
    private CustomerRespository customerRespository;


    /**
     * @param customerDto - CustomerDto Object
     */
    @Override
    public void createAccount(CustomerDto customerDto) {

        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());

        //check if customer already exists in database
        Optional<Customer> optionalCustomer = customerRespository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer already registered with given mobile number: "+customerDto.getMobileNumber());
        }

        Customer savedCustomer = customerRespository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));

    }

    /**
     * @param customer - Customer Object
     * @return the new account details
     */
    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }
}
