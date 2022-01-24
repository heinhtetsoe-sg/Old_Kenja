<?php

require_once("for_php7.php");

require_once('knjm836wModel.inc');
require_once('knjm836wQuery.inc');

class knjm836wController extends Controller
{
    public $ModelClassName = "knjm836wModel";
    public $ProgramID      = "KNJM836W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjm836wForm1");
                    }
                    break 2;
                case "":
                case "knjm836w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm836wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm836wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm836wCtl = new knjm836wController();
//var_dump($_REQUEST);
