<?php

require_once('for_php7.php');

require_once('knjd135bModel.inc');
require_once('knjd135bQuery.inc');

class knjd135bController extends Controller {
    var $ModelClassName = "knjd135bModel";
    var $ProgramID      = "KNJD135B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "clear":
                    $this->callView("knjd135bForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD135B/knjd135bindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd135bindex.php?cmd=edit";
                    $args["cols"] = "40%,*";
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
$knjd135bCtl = new knjd135bController;
?>
