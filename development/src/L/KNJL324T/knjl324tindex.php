<?php

require_once('for_php7.php');

require_once('knjl324tModel.inc');
require_once('knjl324tQuery.inc');

class knjl324tController extends Controller {
    var $ModelClassName = "knjl324tModel";
    var $ProgramID      = "KNJL324T";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324t":
                    $sessionInstance->knjl324tModel();
                    $this->callView("knjl324tForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl324tForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl324tCtl = new knjl324tController;
?>
