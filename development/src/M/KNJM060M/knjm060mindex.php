<?php

require_once('for_php7.php');

require_once('knjm060mModel.inc');
require_once('knjm060mQuery.inc');

class knjm060mController extends Controller {
    var $ModelClassName = "knjm060mModel";
    var $ProgramID      = "KNJM060M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm060m":
                    $sessionInstance->knjm060mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm060mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm060mForm1");
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
$knjm060mCtl = new knjm060mController;
//var_dump($_REQUEST);
?>
