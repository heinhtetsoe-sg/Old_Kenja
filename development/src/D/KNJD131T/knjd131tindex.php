<?php

require_once('for_php7.php');

require_once('knjd131tModel.inc');
require_once('knjd131tQuery.inc');

class knjd131tController extends Controller {
    var $ModelClassName = "knjd131tModel";
    var $ProgramID      = "KNJD131T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                case "attend":
                    $this->callView("knjd131tForm1");
                    break 2;
                case "subform5": //検定選択
                    $this->callView("knjd131tSubForm5");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD131T/knjd131tindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knjd131tindex.php?cmd=edit";
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
$knjd131tCtl = new knjd131tController;
?>
