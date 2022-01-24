<?php

require_once('for_php7.php');

require_once('knjj091Model.inc');
require_once('knjj091Query.inc');

class knjj091Controller extends Controller {
    var $ModelClassName = "knjj091Model";
    var $ProgramID      = "KNJJ091";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj091Form1");
                    break 2;
                case "edit":
                case "edit2":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj091Form2");
                    break 2;
                case "right_list":
                case "from_list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj091Form1");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
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
                    $sessionInstance->knjj091Model();
                case "":
                    $sessionInstance->knjj091Model();
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/J/KNJJ091/knjj091index.php?cmd=right_list") ."&button=1&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knjj091index.php?cmd=right_list";
                    $args["edit_src"]  = "knjj091index.php?cmd=edit";
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
$knjj091Ctl = new knjj091Controller;
?>
