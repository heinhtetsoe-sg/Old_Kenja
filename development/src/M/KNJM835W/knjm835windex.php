<?php
require_once('knjm835wModel.inc');
require_once('knjm835wQuery.inc');

class knjm835wController extends Controller
{
    public $ModelClassName = "knjm835wModel";
    public $ProgramID      = "KNJM835W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjm835wForm1");
                    }
                    break 2;
                case "":
                case "knjm835w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm835wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm835wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm835wCtl = new knjm835wController();
//var_dump($_REQUEST);
