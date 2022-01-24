<?php

require_once('for_php7.php');

require_once('knjm391mModel.inc');
require_once('knjm391mQuery.inc');

class knjm391mController extends Controller {
    var $ModelClassName = "knjm391mModel";
    var $ProgramID      = "KNJM391M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm391mForm1");
                    }
                    break 2;
                case "":
                case "knjm391m":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm391mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm391mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm391mCtl = new knjm391mController;
//var_dump($_REQUEST);
?>
