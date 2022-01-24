<?php

require_once('for_php7.php');
require_once('knjp150kModel.inc');
require_once('knjp150kQuery.inc');

class knjp150kController extends Controller {
    var $ModelClassName = "knjp150kModel";
    var $ProgramID      = "knjp150k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp150kForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjp150kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "all_edit":
                case "change_class":
                case "change_sex":
                    $this->callView("knjp150kForm3");
                    break 2;
                case "all_update":
                    $sessionInstance->getAllUpdateModel();
                    $this->callView("knjp150kForm3");
                    break 2;
                case "all_delete":
                    $sessionInstance->getAllDeleteModel();
                    $this->callView("knjp150kForm3");
                    break 2;
                case "right":
                    $args["right_src"] = "knjp150kindex.php?cmd=list";
                    $args["edit_src"] = "knjp150kindex.php?cmd=edit";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame3.html");
                    return;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/P/KNJP150K/knjp150kindex.php?cmd=right") ."&button=1";
                case "back":
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
                    $args["right_src"] = "knjp150kindex.php?cmd=right";
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
$knjp150kCtl = new knjp150kController;
//var_dump($_REQUEST);
?>
