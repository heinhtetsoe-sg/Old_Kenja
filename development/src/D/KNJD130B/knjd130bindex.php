<?php

require_once('for_php7.php');
require_once('knjd130bModel.inc');
require_once('knjd130bQuery.inc');

class knjd130bController extends Controller {
    var $ModelClassName = "knjd130bModel";
    var $ProgramID      = "KNJD130B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd130bForm1");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD130B/knjd130bindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd130bindex.php?cmd=edit";
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
$knjd130bCtl = new knjd130bController;
?>
