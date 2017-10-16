import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static int[] myaddress = {192, 168, 0, 5};
    private static int myport = 6712;
    private static int[] routeraddress = {168, 188, 123, 60};
    private static Scanner scanner = new Scanner(System.in);
    private static int entry = 0;

    public static void main(String[] args) {
        Packet packet = new Packet();
        NAT[] nat = new NAT[10];
        String flag = "Y";
        while (flag.equals("Y") || flag.equals("y")) {
            sender_packet(packet);
            router_private(nat, packet);
            receiver_packet(packet);
            router_public(nat,packet);
            System.out.println("Continue(Y/N)");
            scanner.nextLine();
            flag=scanner.nextLine();
        }
    }

    private static void sender_packet(Packet packet) { // 패킷을 보낼 주소를 입력받아 패킷 전송
        packet.sip = Arrays.copyOf(myaddress, myaddress.length); //내 주소를 패킷의 source IP에 저장한다.
        packet.sport = myport; //port번호를 복사한다.
        System.out.println("Enter destination address");
        String[] destination_address = scanner.nextLine().split(" "); //입력받은 문자를 공백으로 잘라 배열로 저장
        for (int i = 0; i < packet.dip.length; i++) {// String 타입이므로 int타입으로 변환 복사
            packet.dip[i] = Integer.parseInt(destination_address[i]);
        }
        System.out.println("Enter destination port");
        packet.dport = scanner.nextInt();
        System.out.println("Sender Sends");
        print_packet(packet);
    }

    private static void router_private(NAT[] nat, Packet packet) { //공용망을 사설망으로 전환 및 nat에 저장하는 함수
        boolean flag = false;   //중복되는 주소 검사용 변수
        System.out.println("NAT Receives Packet from Private Networks");
        print_packet(packet);
        for (int i = 0; i < ((entry > 10) ? 10 : entry); i++) { //모든 nat를 확인하여 중복되는지 검사
            if(Arrays.equals(nat[i].priip,packet.dip) && nat[i].priport==packet.dport){
                flag=true;
                break;
            }
        }
        if (!flag) {    //새로운 nat라면 new entry임을 알리고 저장한다.
            System.out.println("New Entry\n");
            nat[entry%nat.length]=new NAT();
            nat[entry%nat.length].priip=Arrays.copyOf(packet.dip,packet.dip.length);
            nat[entry%nat.length].priport=packet.dport;
            nat[entry%nat.length].extip=Arrays.copyOf(packet.sip,packet.sip.length);
            nat[entry%nat.length].extport=packet.sport;
            entry++;
        }
        System.out.println("====Current NAT Table Entry====\n");    //nat 출력
        for (int i = 0; i < ((entry > 10) ? 10 : entry); i++) {
            System.out.printf("%d. %d. %d. %d %d %d. %d. %d. %d %d\n", nat[i].priip[0], nat[i].priip[1], nat[i].priip[2], nat[i].priip[3], nat[i].priport, nat[i].extip[0], nat[i].extip[1], nat[i].extip[2], nat[i].extip[3], nat[i].extport);
        }
        System.out.println("\nNAT Sends Packet to External Network");
        packet.sip=Arrays.copyOf(routeraddress,routeraddress.length); //sip 와 sport를 라우터의 주소와 포트로 변경
        packet.sport=myport;
        print_packet(packet);
    }

    private static void receiver_packet(Packet packet) { //패킷을 받고 주소를 바꿔 보내는 역할을 하는 함수
        System.out.println("Receiver Receives Packet");
        print_packet(packet);
        System.out.println("Receiver Sends Packet");
        int[] tempIP=Arrays.copyOf(packet.sip,packet.sip.length);   //ip와 port swap
        packet.sip=Arrays.copyOf(packet.dip,packet.dip.length);
        packet.dip=Arrays.copyOf(tempIP,tempIP.length);
        int tempPort=packet.sport;
        packet.sport=packet.dport;
        packet.dport=tempPort;
        print_packet(packet);
    }

    private static void router_public(NAT[] nat, Packet packet) {   //사설망을 공용망으로 변경하는 함수
        System.out.println("NAT Receives Packet from External Network");
        print_packet(packet);
        System.out.println("NAT Sends Packet to Private Network");
        for(int i=0;i<((entry>10)?10:entry);i++){   //모든 nat를 확인하여 테이블의 priip와 sip를 확인한
            if(Arrays.equals(nat[i].priip,packet.sip)){
                packet.sip=Arrays.copyOf(nat[i].extip,nat[i].extip.length);
                packet.sport=nat[i].extport;
                break;
            }
        }
        print_packet(packet);

    }

    private static void print_packet(Packet packet) {
        System.out.println("\n====Packet information====");
        System.out.printf("Source IP : %d. %d. %d. %d\n", packet.sip[0], packet.sip[1], packet.sip[2], packet.sip[3]);
        System.out.printf("Dest   IP : %d. %d. %d. %d\n", packet.dip[0], packet.dip[1], packet.dip[2], packet.dip[3]);
        System.out.println("Source PORT : " + packet.sport);
        System.out.println("Dest   PORT : " + packet.dport);
        System.out.println();
    }

    static class Packet {
        int[] sip = new int[4];
        int[] dip = new int[4];
        int sport;
        int dport;
    }

    static class NAT {
        int[] priip = new int[4];
        int priport;
        int[] extip = new int[4];
        int extport;
    }
}
