#include<cstdio>
#include<cstring>
#include<algorithm>
using namespace std;
int k;
char str[205];
struct bignum
{
	int x[205];
	bignum(){memset(x,0,sizeof(x));}
}n,tmp,mul,ans;
bignum operator *(bignum a,bignum b)//特化过的高精乘 只取后k位
{
	bignum ans;
	for(int i=0;i<k;i++)
		for(int j=0;j<k;j++)
			ans.x[i+j]+=a.x[i]*b.x[j];
	for(int i=0;i<k;i++)ans.x[i+1]+=ans.x[i]/10,ans.x[i]%=10;
	for(int i=k;i<205;i++)ans.x[i]=0;
	return ans;
}
bignum operator *(bignum a,int b)//这个高精乘低精是ans专用的233
{
	for(int i=0;i<=200;i++)a.x[i]*=b;
	for(int i=0;i<=200;i++)a.x[i+1]+=a.x[i]/10,a.x[i]%=10;
	return a;
}
int main()
{
	scanf("%s %d",str,&k);
	ans.x[0]=1;
	int len=strlen(str);
	for(int i=0;i<k;i++)
		n.x[i]=str[len-i-1]-'0';
	mul=n;
	for(int i=0;i<k;i++)
	{
		bignum tmp=n;
		int j=1,flag=1;
		for(j=1;j<=10;j++)
		{
			tmp=tmp*mul;
			if(tmp.x[i]==n.x[i])
			{
				ans=ans*j;
				flag=0;
				break;
			}
		}
		if(flag)
			return puts("-1"),0;
		tmp=mul;
		for(int k=1;k<j;k++)
			mul=mul*tmp;
	}
	len=200;
	while(ans.x[len]==0&&len>=1)len--;
	for(;len>=0;len--)putchar(ans.x[len]+'0');
}
