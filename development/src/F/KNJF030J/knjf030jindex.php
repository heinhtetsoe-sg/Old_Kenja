<?php

require_once('for_php7.php');

require_once('knjf030jModel.inc');
require_once('knjf030jQuery.inc');

class knjf030jController extends Controller
{
    public $ModelClassName = "knjf030jModel";
    public $ProgramID      = "KNJF030J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030j":                      //メニュー画面もしくはSUBMITした場合
                case "change_class":                  //クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf030jModel();//コントロールマスタの呼び出し
                    $this->callView("knjf030jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf030jCtl = new knjf030jController();
