package com.spark.accounts.service.impl;

import com.spark.accounts.constants.AccountsConstants;
import com.spark.accounts.dto.AccountsDto;
import com.spark.accounts.dto.CustomerDto;
import com.spark.accounts.entity.Accounts;
import com.spark.accounts.entity.Customer;
import com.spark.accounts.exception.CustomerAlreadyExistsException;
import com.spark.accounts.exception.ResourceNotFoundException;
import com.spark.accounts.mapper.AccountsMapper;
import com.spark.accounts.mapper.CustomerMapper;
import com.spark.accounts.repository.AccountsRepository;
import com.spark.accounts.repository.CustomerRespository;
import com.spark.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
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

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Accounts Details based on a given mobileNumber
     */
    @Override
    public CustomerDto fetchAccountDetails(String mobileNumber) {
        Customer customer =customerRespository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobile number", mobileNumber));
        Accounts account = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customer id", customer.getCustomerId().toString()));

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(account, new AccountsDto()));

        return customerDto;

    }

    /**
     * @param customerDto - CustomerDto Object
     * @return boolean indicating if the update of Account details is successful or not
     */
    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto =  customerDto.getAccountsDto();
        if(accountsDto != null) {
            Accounts account = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "account number", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, account);
            account = accountsRepository.save(account);

            Long customerId = account.getCustomerId();
            Customer customer =customerRespository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "customer id", customerId.toString()));
            CustomerMapper.mapToCustomer(customerDto, customer);
            customerRespository.save(customer);
            isUpdated =true;

        }
        return isUpdated;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of Account details is successful or not
     */
    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer =customerRespository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobile number", mobileNumber));

        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRespository.deleteById(customer.getCustomerId());
        return true;
    }

}
