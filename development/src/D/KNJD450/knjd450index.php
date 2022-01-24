<?php

require_once('for_php7.php');
require_once('knjd450Model.inc');
require_once('knjd450Query.inc');

class knjd450Controller extends Controller {
    var $ModelClassName = "knjd450Model";
    var $ProgramID      = "KNJD450";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "set":
                case "change":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd450Form1");
                    break 2;
                case "add":
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    if ($programpath == ""){
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GUI/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD450/knjd450index.php?cmd=edit") ."&button=1" ."&SES_FLG=2" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GUI/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode($programpath."/knjd450index.php?cmd=edit") ."&button=1" ."&SES_FLG=2" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knjd450index.php?cmd=edit";
                    $args["cols"] = "18%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd450Ctl = new knjd450Controller;
?>
