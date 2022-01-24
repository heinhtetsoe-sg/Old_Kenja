<?php
require_once('for_php7.php');

require_once('knjd614dModel.inc');
require_once('knjd614dQuery.inc');

class knjd614dController extends Controller
{
    public $ModelClassName = "knjd614dModel";
    public $ProgramID      = "KNJD614D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd614d":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knjd614dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd614dForm1");
                    exit;
                case "save_setting":
                    $sessionInstance->knjd614dModel();        //コントロールマスタの呼び出し
                    if (!$sessionInstance->saveSetting()) {
                        $this->callView("knjd614dForm1");
                    }
                    break 2;
                case "load_setting":
                    $sessionInstance->knjd614dModel();        //コントロールマスタの呼び出し
                    $sessionInstance->loadSetting();
                    $this->callView("knjd614dForm1");
                    break 2;
                case "del_setting":
                    $sessionInstance->knjd614dModel();        //コントロールマスタの呼び出し
                    if (!$sessionInstance->delSetting()) {
                        $this->callView("knjd614dForm1");
                    }
                    break 2;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd614dForm1");
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
$knjd614dCtl = new knjd614dController();
//var_dump($_REQUEST);
