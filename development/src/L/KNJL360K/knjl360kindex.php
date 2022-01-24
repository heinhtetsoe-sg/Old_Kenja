<?php

require_once('for_php7.php');

require_once('knjl360kModel.inc');
require_once('knjl360kQuery.inc');

class knjl360kController extends Controller {
    var $ModelClassName = "knjl360kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl360kForm1");
                    }
                    break 2;
                case "":
                case "knjl360k":
                    $sessionInstance->knjl360kModel();
                    $this->callView("knjl360kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl360kCtl = new knjl360kController;
//var_dump($_REQUEST);
?>
