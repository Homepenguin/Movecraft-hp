package net.countercraft.movecraft.commands;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.util.TopicPaginator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class CraftTypeCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CraftType type;
        int page;
        if(args.length == 0 || (args.length == 1 && tryParsePage(args[0]).isPresent())){
            Optional<CraftType> typeQuery = tryGetCraftFromPlayer(commandSender);
            if(typeQuery.isEmpty()){
                commandSender.sendMessage("You must supply a craft type!");
                return true;
            }
            type = typeQuery.get();
            page = args.length == 0 ? 1 : tryParsePage(args[0]).getAsInt();
        } else {
            type = CraftManager.getInstance().getCraftTypeFromString(args[0]);
            if(args.length > 1){
                OptionalInt pageQuery = tryParsePage(args[1]);
                if(pageQuery.isEmpty()){
                    commandSender.sendMessage("Argument " + args[1] + " must be a page number");
                    return true;
                }
                page = pageQuery.getAsInt();
            } else {
                page = 1;
            }
        }
        if(!commandSender.hasPermission("movecraft." + type.getCraftName() + ".pilot")){
            commandSender.sendMessage("You don't have permission for that craft type!");
            return true;
        }
        sendTypePage(type, page, commandSender);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length !=1 || !commandSender.hasPermission("movecraft.commands") || !commandSender.hasPermission("movecraft.commands.crafttype"))
            return Collections.emptyList();
        List<String> completions = new ArrayList<>();
        for(CraftType type : CraftManager.getInstance().getCraftTypes())
            if(commandSender.hasPermission("movecraft." + type.getCraftName() + ".pilot"))
                completions.add(type.getCraftName());
        List<String> returnValues = new ArrayList<>();
        for(String completion : completions)
            if(completion.toLowerCase().startsWith(strings[strings.length-1].toLowerCase()))
                returnValues.add(completion);
        return returnValues;
    }

    private void sendTypePage(@NotNull CraftType type, int page, @NotNull  CommandSender commandSender){
        TopicPaginator paginator = new TopicPaginator("Type Info");
        for(var entry : type.getTypeData().getBackingData().entrySet()){
            paginator.addLine(entry.getKey() + ": " + entry.getValue());
        }
        if(!paginator.isInBounds(page)){
            commandSender.sendMessage(String.format("Page %d is out of bounds.", page));
            return;
        }
        for(String line : paginator.getPage(page))
            commandSender.sendMessage(line);
    }

    private @NotNull OptionalInt tryParsePage(@NotNull String encoded){
        try {
            return OptionalInt.of(Integer.parseInt(encoded));
        }catch(NumberFormatException e){
            return OptionalInt.empty();
        }
    }

    @NotNull
    private Optional<CraftType> tryGetCraftFromPlayer(CommandSender commandSender){
        if (!(commandSender instanceof Player)) {
            return Optional.empty();
        }
        Craft c = CraftManager.getInstance().getCraftByPlayer((Player) commandSender);
        if(c == null){
            return Optional.empty();
        }
        return Optional.of(c.getType());
    }
}
