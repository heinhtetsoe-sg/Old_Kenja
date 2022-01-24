<?php

require_once('for_php7.php');
require_once('knjp100kModel.inc');
require_once('knjp100kQuery.inc');

class knjp100kController extends Controller {
    var $ModelClassName = "knjp100kModel";
    var $ProgramID      = "knjp100k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp100kForm2");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "all_add":
                    $sessionInstance->getAllAddModel();
                    $this->callView("knjp100kForm3");
                    break 2;
                case "all_update":
                    $sessionInstance->getAllUpdateModel();
                    $this->callView("knjp100kForm3");
                    break 2;
                case "all_delete":
                    $sessionInstance->getAllDeleteModel();
                    $this->callView("knjp100kForm3");
                    break 2;
                case "list":
                    $this->callView("knjp100kForm1");
                    break 2;
                case "all_edit":
                case "change_class":
                case "selectChange":
                    $this->callView("knjp100kForm3");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
                    $args["right_src"] = "knjp100kindex.php?cmd=list";
                    $args["edit_src"] = "knjp100kindex.php?cmd=edit";
                    $args["rows"] = "42%,58%";
                    View::frame($args,"frame3.html");
                    return;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/P/KNJP100K/knjp100kindex.php?cmd=right") ."&button=1";
                case "back":
#                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
                    $args["right_src"] = "knjp100kindex.php?cmd=right";
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
$knjp100kCtl = new knjp100kController;
//var_dump($_REQUEST);
?>
