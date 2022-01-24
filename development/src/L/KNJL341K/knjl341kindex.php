<?php

require_once('for_php7.php');

require_once('knjl341kModel.inc');
require_once('knjl341kQuery.inc');

class knjl341kController extends Controller {
    var $ModelClassName = "knjl341kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード---NO001
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl341kForm1");
                    }
                    break 2;
                case "":
                case "knjl341k":
                    $sessionInstance->knjl341kModel();
                    $this->callView("knjl341kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl341kCtl = new knjl341kController;
//var_dump($_REQUEST);
?>
