package org.yproject.pet.core.application.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yproject.pet.core.domain.transaction.Currency;
import org.yproject.pet.core.domain.transaction.Transaction;
import org.yproject.pet.core.infrastructure.generator.identity.IdGenerator;
import org.yproject.pet.core.util.RandomUtils;
import org.yproject.pet.core.util.TransactionRandomUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.yproject.pet.core.util.RandomUtils.*;
import static org.yproject.pet.core.util.TransactionRandomUtils.randomTransaction;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @InjectMocks
    private TransactionServiceImpl underTest;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private TransactionStorage transactionStorage;

    @Test
    void create() {
        final var userId = randomShortString();
        final var description = randomShortString();
        final var amount = randomDouble();
        final var currency = randomFrom(Currency.values()).name();
        final var createTime = randomInstant().toEpochMilli();

        final var id = randomShortString();
        final var transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(idGenerator.get())
                .thenReturn(id);
        when(transactionStorage.save(any()))
                .thenReturn(id);

        final var result = underTest.create(userId, description, amount, currency, createTime);

        verify(transactionStorage).save(transactionArgumentCaptor.capture());

        assertThat(transactionArgumentCaptor.getValue())
                .returns(id, Transaction::id)
                .returns(description, Transaction::description)
                .returns(amount, Transaction::amount)
                .returns(currency, transaction -> transaction.currency().name())
                .returns(userId, Transaction::creatorUserId)
                .returns(createTime, transaction -> transaction.createTime().toEpochMilli());
        assertThat(result).isEqualTo(id);

    }

    @Test
    void modify() {
        final var modifyId = randomShortString();
        final var userId = randomShortString();
        final var description = randomShortString();
        final var amount = randomDouble();
        final var currency = randomFrom(Currency.values()).name();
        final var createTime = randomInstant().toEpochMilli();

        final var oldTransaction = randomTransaction();

        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionStorage.retrieveOneByIdAndUserId(anyString(), anyString()))
                .thenReturn(Optional.of(oldTransaction));
        when(transactionStorage.save(any()))
                .thenReturn(userId);

        underTest.modify(userId, modifyId, description, amount, currency, createTime);

        then(transactionStorage).should().save(transactionArgumentCaptor.capture());

        assertThat(transactionArgumentCaptor.getValue())
                .returns(oldTransaction.id(), Transaction::id)
                .returns(description, Transaction::description)
                .returns(amount, Transaction::amount)
                .returns(currency, transaction -> transaction.currency().name())
                .returns(oldTransaction.creatorUserId(), Transaction::creatorUserId)
                .returns(createTime, transaction -> transaction.createTime().toEpochMilli());
    }

    @Test
    void modify_throw_not_existed_exception() {
        final var modifyId = randomShortString();
        final var userId = randomShortString();
        final var description = randomShortString();
        final var amount = randomDouble();
        final var currency = randomFrom(Currency.values()).name();
        final var createTime = randomInstant().toEpochMilli();

        when(transactionStorage.retrieveOneByIdAndUserId(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(TransactionService.TransactionNotExisted.class,
                () -> underTest.modify(userId, modifyId, description, amount, currency, createTime));

        then(transactionStorage).should().retrieveOneByIdAndUserId(modifyId, userId);
        then(transactionStorage).should(never()).save(any());
    }

    @Test
    void delete() {
        final var transactionIds = randomShortList(RandomUtils::randomShortString);
        final var userId = randomShortString();

        underTest.delete(transactionIds, userId);

        then(transactionStorage).should().deleteByIdsAndUserId(transactionIds, userId);
    }

    @Test
    void retrieveAll() {
        final var userId = randomShortString();
        final var transactions = randomShortList(TransactionRandomUtils::randomTransaction);
        when(transactionStorage.retrieveAllByUserId(any()))
                .thenReturn(transactions);

        final var result = underTest.retrieveAll(userId);

        then(transactionStorage).should().retrieveAllByUserId(userId);
        assertThat(result).hasSameSizeAs(transactions);
    }

    @Test
    void retrieve() {
        final var userId = randomShortString();
        final var transactionId = randomShortString();
        final var transaction = randomTransaction(transactionId);
        when(transactionStorage.retrieveOneByIdAndUserId(any(), any()))
                .thenReturn(Optional.of(transaction));

        final var result = underTest.retrieve(userId, transactionId);

        then(transactionStorage).should().retrieveOneByIdAndUserId(transactionId, userId);
        assertThat(result)
                .returns(transactionId, RetrieveTransactionDto::id)
                .returns(transaction.description(), RetrieveTransactionDto::description)
                .returns(transaction.amount(), RetrieveTransactionDto::amount)
                .returns(transaction.currency(), RetrieveTransactionDto::currency)
                .returns(transaction.createTime(), RetrieveTransactionDto::createTime);
    }

    @Test
    void retrieve_throw_not_existed_exception() {
        final var userId = randomShortString();
        final var transactionId = randomShortString();
        when(transactionStorage.retrieveOneByIdAndUserId(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(TransactionService.TransactionNotExisted.class, () -> underTest.retrieve(userId, transactionId));
    }

}
