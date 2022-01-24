<?php

require_once('for_php7.php');

require_once('knje370fModel.inc');
require_once('knje370fQuery.inc');

class knje370fController extends Controller {
    var $ModelClassName = "knje370fModel";
    var $ProgramID      = "KNJE370F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knje370fForm1");
                    break 2;
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE370F/knje370findex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}"."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["right_src"] = "knje370findex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knje370fCtl = new knje370fController;
?>
