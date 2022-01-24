<?php

require_once('for_php7.php');
require_once('knjj200Model.inc');
require_once('knjj200Query.inc');

class knjj200Controller extends Controller {
    var $ModelClassName = "knjj200Model";
    var $ProgramID      = "KNJJ200";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjj200Form1");
                    break 2;
                case "edit":
                case "reset":
                    $this->callView("knjj200Form2");
                    break 2;
                case "right_list":
                    $this->callView("knjj200Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjj200Model();
                case "":
                    $sessionInstance->knjj200Model();
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/J/KNJJ200/knjj200index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjj200index.php?cmd=right_list";
                    $args["edit_src"]  = "knjj200index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj200Ctl = new knjj200Controller;
?>
