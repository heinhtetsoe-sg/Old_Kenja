<?php

require_once('for_php7.php');

require_once('knjd154kModel.inc');
require_once('knjd154kQuery.inc');

class knjd154kController extends Controller {
    var $ModelClassName = "knjd154kModel";
    var $ProgramID      = "KNJD154K";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":        //データ出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd154kForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "clear";
                case "knjd154k";
                    $sessionInstance->knjd154kModel();
                    $this->callView("knjd154kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154kCtl = new knjd154kController;
?>
