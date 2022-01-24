<?php

require_once('for_php7.php');

require_once('knjp121kModel.inc');
require_once('knjp121kQuery.inc');

class knjp121kController extends Controller {
    var $ModelClassName = "knjp121kModel";
    var $ProgramID      = "knjp121k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp121kForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjp121kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "all_edit":
                case "change_class":
                case "change_sex":
                    $this->callView("knjp121kForm3");
                    break 2;
                case "all_update":
                    $sessionInstance->getAllUpdateModel();
                    $this->callView("knjp121kForm3");
                    break 2;
                case "right":
                    $args["right_src"] = "knjp121kindex.php?cmd=list";
                    $args["edit_src"] = "knjp121kindex.php?cmd=edit";
                    $args["rows"] = "44%,56%";
                    View::frame($args,"frame3.html");
                    return;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/P/KNJP121K/knjp121kindex.php?cmd=right") ."&button=1";
                case "back":
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
                    $args["right_src"] = "knjp121kindex.php?cmd=right";
                    $args["cols"] = "25%,*";
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
$knjp121kCtl = new knjp121kController;
//var_dump($_REQUEST);
?>
