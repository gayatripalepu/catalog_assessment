package kits.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class Point {
    private final int x;
    private final BigInteger y;

    public Point(int x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }
}


interface ValueDecoder {
    BigInteger decode(String value, int base);
}


class BaseValueDecoder implements ValueDecoder {
    @Override
    public BigInteger decode(String value, int base) {
        return new BigInteger(value, base);
    }
}


class LagrangeInterpolator {
    public BigInteger interpolateConstant(List<Point> points) {
        BigInteger constant = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            Point pi = points.get(i);
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    Point pj = points.get(j);
                    numerator = numerator.multiply(BigInteger.valueOf(-pj.getX()));
                    denominator = denominator.multiply(BigInteger.valueOf(pi.getX() - pj.getX()));
                }
            }

            BigInteger term = numerator.multiply(pi.getY()).divide(denominator);
            constant = constant.add(term);
        }

        return constant;
    }
}


class JsonInputReader {
    private final ValueDecoder decoder;
    private final String filePath;

    public JsonInputReader(ValueDecoder decoder, String filePath) {
        this.decoder = decoder;
        this.filePath = filePath;
    }

    public List<Point> readInput(int k) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(filePath));

        List<Point> points = new ArrayList<>();
        rootNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if (!key.equals("keys")) {
                int x = Integer.parseInt(key);
                JsonNode pointNode = entry.getValue();
                int base = pointNode.get("base").asInt();
                String value = pointNode.get("value").asText();
                BigInteger y = decoder.decode(value, base);
                points.add(new Point(x, y));
            }
        });


        points.sort(Comparator.comparingInt(Point::getX));
        return points.subList(0, k);
    }
}


public class ShamirSecretSharing {
    public static void main(String[] args) {
        try {
            
            String[] filePaths = {"input.json", "input1.json"};

            ValueDecoder decoder = new BaseValueDecoder();
            LagrangeInterpolator interpolator = new LagrangeInterpolator();

            for (String filePath : filePaths) {
                JsonInputReader inputReader = new JsonInputReader(decoder, filePath);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode keysNode = mapper.readTree(new File(filePath)).get("keys");
                int k = keysNode.get("k").asInt();

                List<Point> points = inputReader.readInput(k);
                BigInteger constant = interpolator.interpolateConstant(points);

               
                System.out.println("Secret (Constant Term) for " + filePath + ": " + constant);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}