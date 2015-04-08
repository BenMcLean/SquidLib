package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;

/**
 * Sample output: {@code
############################################################
##..############~~###...##########..######......#.....^...##
#...####.....###.~.+....#########....#####......#.....+....#
#...####.....###.~.+..........###....#####......#..####....#
#......#.....###..##..........####..#######....##^.####....#
#......#.....###..##...^............########++###++####....#
#......#..........##.^.....................+.......#.......#
#......#..........###..^......###..........+.......#.......#
##.....#####......###........#######++######..######..######
##.....#####......#####...^.#####......#####..######++######
####..######++############++#####......###.....#####......##
####.....###..######........#####......##........####.....##
####......##..####..........^~~~#......##........#..##.....#
######....##..####....#####.~~~~#########..####..#...##....#
#.####....##..######..+...#++#~~~~~~~~###++#..#..#....###..#
#..###..^.##..######..+...#..#~~~~~~~~..+.~#..#..#...^.#####
#..###..........##....#...#..#.~#~~~~~..~~~#...^........####
#..###..........##....#####++####~~~~####~~#...............#
#..+.....#####..####.........####~~~~####~~#..######.......#
#..+..^..#####..#####.......#####~~~~####~~#..^..###.....###
############.....#####..#########~~~~~###~~#.....#####..####
############^....#####..###~~~~#~~~~#####~~#.....#####...###
#....#######.....#..##....#~~~~~~~~~~~~~~~~#..#..######...##
#....#....#####..#..##....#~~~~~~~~~~~~~~~~#..#..#...###..##
###..#....#####..#..####..#~~^~#~~##########..####....##..##
#...^...............####..######~~#~~~~##.............##++##
#.........................#~~~~~~~~~~~~##.............##...#
#.......^.######..........#~~~~~~~~~~~~##.....^.##....##...#
#........#######...#####..######^~#~~~~##.......###...#....#
###.....###...##...#####++######~~#########.....###.......##
######..###...##...###..^..#####~~#########.....###......###
#.......###...########.....#####~~###....#....#######...####
#.............########.....#####~~~~#............+..#..#####
#.............^.....###...######~~~~#............+..########
#++#######..........####++########~~#....#..######...###...#
#...#...##...............^########~~######++#....#....##..^#
#...#...##................#.~~~~~~~~#............#.....#...#
#...#...###.........#.....#~~~~~~~~~#............##........#
#.......###.......^##++####~~#####~~######..#....##........#
##......#####.....###..+.~~~~#####~~######..#########.....##
##.....#########..###..+.~~~~###..~~.#####..############..##
#.......#..+...#..#############....~...###++############..##
#.......#..+...+............###............^..#....#..^+...#
#.......+..#...+............##....##..........#....#...+...#
##......+..#...#....######..##...####.........#....#..######
################..^.#....#.^.....####.........#^...#.......#
#..+.......+..##....#....#........##..........#....#.......#
#..+.......+..##++###.......##................#....+..#..^.#
####++######......###.......####......######..#....+..#....#
####..######......########..#####....#######..######..#....#
####..######.^....#####.~...##############...^.#####......##
#........##........+..##.....#############...#######......##
#........##........+..###.......###########++#...#........##
#........##^...#####..###........#####~~~~#..#...#.....^..##
#........##....#####++###........####~~~~~#..#^..#...#..####
#..########....###....###.........##~~~~~~#..#...#...#..####
#..............###^...###..........~~~~~~~...........#..####
#..............###....##.....##.....~~~~~~.......^...#..####
###########.....#######......##.^...~~.##~.......#####..####
############################################################
 *
 * }
 * Created by Tommy Ettinger on 4/8/2015.
 */
public class DungeonGeneratorTest {
    public static void main( String[] args )
    {
        DungeonGenerator dungeonGenerator = new DungeonGenerator(60, 60);
        dungeonGenerator.AddDoors(30, true);
        dungeonGenerator.AddWater(20);
        dungeonGenerator.AddTraps(2);
        dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        System.out.println(dungeonGenerator.toString());

    }
}