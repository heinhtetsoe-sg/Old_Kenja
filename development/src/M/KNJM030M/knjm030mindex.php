<?php

require_once('for_php7.php');

require_once('knjm030mModel.inc');
require_once('knjm030mQuery.inc');

class knjm030mController extends Controller {
    var $ModelClassName = "knjm030mModel";
    var $ProgramID      = "KNJM030M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm030m":
                    $sessionInstance->knjm030mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm030mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm030mForm1");
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
$knjm030mCtl = new knjm030mController;
//var_dump($_REQUEST);
?>
