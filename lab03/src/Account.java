/**
 * This class represents a bank account whose current balance is a non-negative
 * amount in US dollars.
 */
public class Account {

    private int balance;

    private final Account parentAccount;

    /** Initialize an account with the given balance. */
    public Account(int balance) {
        this(balance, null);  // * null per spec
    }
    public Account(int balance, Account parentAccount) {
        if (balance < 0) {
            throw new IllegalArgumentException("Cannot initialize account with negative balance.");
        }
        this.balance = balance;
        this.parentAccount = parentAccount;
    }

    /** Returns the balance for the current account. */
    public int getBalance() {
        return this.balance;
    }

    /** Deposits amount into the current account. */
    public boolean deposit(int amount) {
        if (amount < 0) {
            System.out.println("Cannot deposit negative amount.");
            return false;
        } else {
            this.balance += amount;
            return true;
        }
    }

    /**
     * Subtract amount from the account if possible. If the subtracting amount
     * leaves a negative balance, print an error message and leave the
     * balance unchanged.
     */
    public synchronized boolean withdraw(int amount) {
        // DONE
        if (amount < 0) {
            System.out.println("Cannot withdraw negative amount.");
            return false;
        }
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        if (this.canProtectOverdraft(amount)) {
            System.out.println("Overdraft protected");
            this.parentAccount.withdraw(amount - this.balance);
            this.balance = 0;
            return true;
        }
        System.out.println("Insufficient funds");
        return false;
    }

    private boolean canProtectOverdraft(int amount) {
        if (this.parentAccount != null) {
            int parentBalance = this.parentAccount.getBalance();
            if (parentBalance >= amount) {
                return true;
            } else {
                System.out.println("Parent account has insufficient funds for overdraft.");
                return this.parentAccount.canProtectOverdraft(amount - parentBalance);
            }
        } else {
            System.out.println("No parent account to protect overdraft.");
        }
        return false;
    }

    /**
     * Merge the account other into this account by removing all money from other
     * and depositing it into this account.
     */
    public synchronized boolean merge(Account other) {
        // DONE
        if (this == other) {
            return true;
        }
        int amount = other.getBalance();
        if (other.withdraw(amount)) {
            this.deposit(amount);
            return true;
        } else {
            System.out.println("Cannot merge accounts with insufficient funds.");
        }
        return false;
    }
}
