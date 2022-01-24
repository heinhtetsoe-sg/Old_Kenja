<?php

require_once('for_php7.php');

require_once('knjd135cModel.inc');
require_once('knjd135cQuery.inc');

class knjd135cController extends Controller {
    var $ModelClassName = "knjd135cModel";
    var $ProgramID      = "KNJD135C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjd135cForm1");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD135C/knjd135cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd135cindex.php?cmd=edit";
                    $args["cols"] = "20%,*";
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
$knjd135cCtl = new knjd135cController;
?>
