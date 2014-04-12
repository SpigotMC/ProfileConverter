package org.spigotmc;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.spout.nbt.CompoundTag;
import org.spout.nbt.StringTag;
import org.spout.nbt.Tag;
import org.spout.nbt.stream.NBTInputStream;

public class ProfileConverter
{

    public static void main(String[] args) throws Exception
    {
        String line;
        final BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
        System.out.println( "Welcome to ProfileConverter by SpigotMC. This tool is designed to convert UUID based player data files back to their named counterparts." );
        System.out.println( "It comes with no warranty, express or implied, and as such all responsibility rests with you the user. Backups are essential." );
        System.out.println( "To begin, simply enter the path to your primary Minecraft world" );

        File worldContainer = null;
        while ( ( line = br.readLine() ) != null )
        {
            worldContainer = new File( line );
            if ( worldContainer.exists() )
            {
                break;
            }
            System.out.println( "The location you entered does not exist, please enter a location which does!" );
        }

        File newFolder = new File( worldContainer, "playerdata" );
        if ( !newFolder.exists() )
        {
            System.out.println( "Could not find any UUID based playerdata in the location you have entered" );
        }

        File oldFolder = new File( worldContainer, "players" );
        if ( !oldFolder.exists() )
        {
            oldFolder.mkdir();
        }

        File[] toConvert = newFolder.listFiles();
        System.out.println( "Discovered " + toConvert.length + " files for conversion. Converting now...." );
        int success = 0, error = 0;
        for ( File sourceFile : toConvert )
        {
            NBTInputStream nbtStream = new NBTInputStream( new FileInputStream( sourceFile ), true );
            try
            {
                Tag root = nbtStream.readTag();
                if ( !( root instanceof CompoundTag ) )
                {
                    throw new RuntimeException( "Does not have root compound tag" );
                }

                Tag bukkit = ( (CompoundTag) root ).getValue().get( "bukkit" );
                if ( !( bukkit instanceof CompoundTag ) )
                {
                    throw new RuntimeException( "Does not have bukkit compound tag" );
                }

                Tag lastKnownName = ( (CompoundTag) bukkit ).getValue().get( "lastKnownName" );
                if ( !( lastKnownName instanceof StringTag ) )
                {
                    throw new RuntimeException( "Does not have lastKnownName string tag" );
                }

                String name = ( (StringTag) lastKnownName ).getValue();
                File destFile = new File( oldFolder, name + ".dat" );
                if ( destFile.exists() )
                {
                    throw new RuntimeException( "Duplicate name " + name );
                }
                Files.copy( sourceFile, destFile );
                success++;
            } catch ( Exception ex )
            {
                System.err.println( "Error converting file " + sourceFile );
                System.err.print( "\t" );
                ex.printStackTrace();
                error++;
            } finally
            {
                nbtStream.close();
            }
        }
        System.out.println( "Successfully converted " + success + " files. Please review all " + error + " errors and thanks for using this tool!" );
    }
}
