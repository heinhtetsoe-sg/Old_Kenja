<?php

require_once('for_php7.php');

require_once('knjp070kModel.inc');
require_once('knjp070kQuery.inc');

class knjp070kController extends Controller {
    var $ModelClassName = "knjp070kModel";
    var $ProgramID      = "knjp070k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit1":
                    $sessionInstance->div = "M";
                    $this->callView("knjp070kForm3");
                    break 2;
                case "edit2":
                    $sessionInstance->div = "S";
                    $this->callView("knjp070kForm5");
                    break 2;
                case "list1":
                    $this->callView("knjp070kForm1");
                    break 2;
                case "list2":
                    $this->callView("knjp070kForm2");
                    break 2;
                case "all_update":
                    $sessionInstance->getAllUpdateModel();
                    $sessionInstance->setCmd("all_edit");
                    break 1;
                case "all_add":
                    $sessionInstance->getAllAddModel();
                    $sessionInstance->setCmd("all_edit");
                    break 1;
                case "all_delete":
                    $sessionInstance->getAllDeleteModel();
                    $sessionInstance->setCmd("all_edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    if ($sessionInstance->div == "M")
                        $sessionInstance->setCmd("edit1");
                    if ($sessionInstance->div == "S")
                        $sessionInstance->setCmd("edit2");
                    break 1;
                case "add":
                    $sessionInstance->getAddModel();
                    if ($sessionInstance->div == "M")
                        $sessionInstance->setCmd("edit1");
                    if ($sessionInstance->div == "S")
                        $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    if ($sessionInstance->div == "M")
                        $sessionInstance->setCmd("edit1");
                    if ($sessionInstance->div == "S")
                        $sessionInstance->setCmd("edit2");
                    break 1;
                case "all_edit":
                case "all_edit2":
                case "change_sex":	//NO001
                    $this->callView("knjp070kForm4");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
                    $sessionInstance->div = "M";
                    $args["top_src"] = "knjp070kindex.php?cmd=list1";
                    $args["mid_src"] = "knjp070kindex.php?cmd=list2";
                    $args["bot_src"] = "knjp070kindex.php?cmd=edit1";
                    $args["rows"] = "35%,35%,*";
                    View::frame($args,"frame5.html");
                    return;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/P/KNJP070K/knjp070kindex.php?cmd=right") ."&button=1";
                case "back":
#                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
                    $args["right_src"] = "knjp070kindex.php?cmd=right";
                    $args["cols"] = "30%,*";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp070kCtl = new knjp070kController;
//var_dump($_REQUEST);
?>
