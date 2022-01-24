<?php
require_once('knjd659gModel.inc');
require_once('knjd659gQuery.inc');

class knjd659gController extends Controller
{
    public $ModelClassName = "knjd659gModel";
    public $ProgramID      = "KNJD659G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd659g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd659gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd659gForm1");
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
$knjd659gCtl = new knjd659gController();
//var_dump($_REQUEST);
