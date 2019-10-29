package poker.eval;

/**
 * Created by James Northrup
 * User: jim
 * Date: Aug 11, 2007
 * Time: 2:38:27 AM
 */
public enum Suit {

    /**
     * <table>
     * <tr><th>Suit     <th> hex  <th> decimal  </tr>
     * <tr><td>Spades   <td> 2260 <td> 9824     </tr>
     * <tr><td>Clubs    <td> 2263 <td> 9827     </tr>
     * <tr><td>Hearts   <td> 2265 <td> 9829     </tr>
     * <tr><td>Diamonds <td> 2266 <td> 9830     </tr>
     * </table>
     */
    CLUBS((char) 0x2263, 'c'),
    DIAMONDS((char) 0x2266, 'd'),
    HEARTS((char) 0x2265, 'h'),
    SPADES((char) 0x2260, 's'),;

    public final char desc;
    public final char unicode;

    private Suit(final char... desc) {
        this.unicode = desc[0];
        this.desc = desc[1];
    }
}
