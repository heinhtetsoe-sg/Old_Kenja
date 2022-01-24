<?php
require_once('knjh442dModel.inc');
require_once('knjh442dQuery.inc');

class knjh442dController extends Controller
{
    public $ModelClassName = "knjh442dModel";
    public $ProgramID      = "KNJH442D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh442d":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjh442dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh442dForm1");
                    exit;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh442dCtl = new knjh442dController;
//var_dump($_REQUEST);
