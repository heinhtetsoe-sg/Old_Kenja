<?php

require_once('for_php7.php');

require_once('knjj183Model.inc');
require_once('knjj183Query.inc');

class knjj183Controller extends Controller {
    var $ModelClassName = "knjj183Model";
    var $ProgramID      = "KNJJ183";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjj183Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/J/KNJJ183/knjj183index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjj183index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
                    View::frame($args);
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjj183Ctl = new knjj183Controller;
?>
