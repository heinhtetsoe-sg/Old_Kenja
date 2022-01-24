<?php

require_once('for_php7.php');

require_once('knjl324mModel.inc');
require_once('knjl324mQuery.inc');

class knjl324mController extends Controller {
    var $ModelClassName = "knjl324mModel";
    var $ProgramID      = "KNJL324M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324m":
                    $sessionInstance->knjl324mModel();
                    $this->callView("knjl324mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl324mForm1");
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
$knjl324mCtl = new knjl324mController;
?>
