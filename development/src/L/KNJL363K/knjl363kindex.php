<?php
require_once('knjl363kModel.inc');
require_once('knjl363kQuery.inc');

class knjl363kController extends Controller {
    var $ModelClassName = "knjl363kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl363kForm1");
                    }
                    break 2;
                case "":
                case "knjl363k":
                    $sessionInstance->knjl363kModel();
                    $this->callView("knjl363kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl363kCtl = new knjl363kController;
//var_dump($_REQUEST);
?>
