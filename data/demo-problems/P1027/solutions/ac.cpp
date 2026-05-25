#include <bits/stdc++.h>
using namespace std;

const int MAXN = 105;
const double INF = 1e18;

struct Point {
    double x, y;
    Point() {}
    Point(double x, double y) : x(x), y(y) {}
};

double dist(Point a, Point b) {
    return sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
}

// 根据矩形三个点求第四个点
Point getFourth(Point a, Point b, Point c) {
    // 判断哪个是直角顶点
    double ab = dist(a, b);
    double ac = dist(a, c);
    double bc = dist(b, c);
    if (fabs(ab * ab + ac * ac - bc * bc) < 1e-6) {
        // a是直角顶点，则d = b + c - a
        return Point(b.x + c.x - a.x, b.y + c.y - a.y);
    }
    if (fabs(ab * ab + bc * bc - ac * ac) < 1e-6) {
        // b是直角顶点
        return Point(a.x + c.x - b.x, a.y + c.y - b.y);
    }
    // c是直角顶点
    return Point(a.x + b.x - c.x, a.y + b.y - c.y);
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    cout << fixed << setprecision(1);

    int n;
    cin >> n;
    while (n--) {
        int S, A, B;
        double t;
        cin >> S >> t >> A >> B;
        A--; B--; // 转为0-index

        vector<Point> city[MAXN]; // 每个城市的四个机场
        double T[MAXN];

        for (int i = 0; i < S; i++) {
            double x1, y1, x2, y2, x3, y3, ti;
            cin >> x1 >> y1 >> x2 >> y2 >> x3 >> y3 >> ti;
            T[i] = ti;
            Point p1(x1, y1), p2(x2, y2), p3(x3, y3);
            Point p4 = getFourth(p1, p2, p3);
            city[i].push_back(p1);
            city[i].push_back(p2);
            city[i].push_back(p3);
            city[i].push_back(p4);
        }

        // 总机场数
        int total = S * 4;
        // 邻接矩阵存图
        vector<vector<double>> g(total, vector<double>(total, INF));
        for (int i = 0; i < total; i++) g[i][i] = 0;

        // 建图
        for (int i = 0; i < S; i++) {
            // 城市内高铁
            for (int a = 0; a < 4; a++) {
                for (int b = a + 1; b < 4; b++) {
                    double d = dist(city[i][a], city[i][b]) * T[i];
                    int u = i * 4 + a;
                    int v = i * 4 + b;
                    g[u][v] = g[v][u] = min(g[u][v], d);
                }
            }
            // 城市间飞机
            for (int j = i + 1; j < S; j++) {
                for (int a = 0; a < 4; a++) {
                    for (int b = 0; b < 4; b++) {
                        double d = dist(city[i][a], city[j][b]) * t;
                        int u = i * 4 + a;
                        int v = j * 4 + b;
                        g[u][v] = g[v][u] = min(g[u][v], d);
                    }
                }
            }
        }

        // Floyd
        for (int k = 0; k < total; k++) {
            for (int i = 0; i < total; i++) {
                if (g[i][k] == INF) continue;
                for (int j = 0; j < total; j++) {
                    if (g[k][j] == INF) continue;
                    g[i][j] = min(g[i][j], g[i][k] + g[k][j]);
                }
            }
        }

        double ans = INF;
        for (int a = 0; a < 4; a++) {
            for (int b = 0; b < 4; b++) {
                int u = A * 4 + a;
                int v = B * 4 + b;
                ans = min(ans, g[u][v]);
            }
        }
        cout << ans << "\n";
    }
    return 0;
}