## 题目背景


## 题目描述
The advice to "buy low" is half the formula to success in the stock market. But to be considered a great investor you must also follow this problems' advice:

"Buy low, buy lower"

That is, each time you buy a stock, you must purchase at a lower price than the previous time you bought it. The more times you buy at a lower price than before, the better! Your goal is to see how many times you can continue purchasing at ever lower prices.

Unlike the regular stock market, you will be given the daily selling prices of a stock over a period of time. You can choose to buy stock on any of the days. Each time you choose to buy, the price must be lower than the previous time you bought stock. Write a program which identifies which days you should buy stock in order to maximize the number of times you buy.

For example, suppose on successive days stock is selling like this:

$$
\def\arraystretch{1.5}
\begin{array}{|c|c|c|c|c|c|c|c|c|c|c|c|c|}\hline
\textsf{Day} & 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 & 9 & 10 & 11 & 12 \cr\hline
\textsf{Price} & 68 & 69 & 54 & 64 & 68 & 64 & 70 & 67 & 78 & 62& 98 & 87 \cr\hline
\end{array}$$

In the example above, the best investor (by this problem, anyway) can buy at most four times if they purchase at a lower price each time. One four day sequence (there might be others) of acceptable buys is:

$$
\def\arraystretch{1.5}
\begin{array}{|c|c|c|c|c|}\hline
\textsf{Day} & 2 & 5 & 6 & 10 \cr\hline
\textsf{Price} & 69 & 68 & 64 & 62 \cr\hline
\end{array}
$$

## 输入格式
The first line of file INPUT.TXT contains the number of days $1\le n\le 5,000$ for which prices are available. Each of N subsequent lines contains the price for that day ($1 \le\text{"each" price }\le 32767$).

## 输出格式
Print to the file OUTPUT.TXT two integers on a single line:

- the length of the longest sequence of decreasing prices
- the number of sequences that have this length


In counting the number of solutions, two potential solutions are considered the same (and would only count as one solution) if they repeat the same string of decreasing prices, that is, if they "look the same" when the successive prices are compared. Thus, two different sequence of "buy" days could produce the same string of decreasing prices and be counted as only a single solution.

## 提示

