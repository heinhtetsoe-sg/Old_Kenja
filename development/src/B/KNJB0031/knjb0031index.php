<?php

require_once('for_php7.php');

require_once('knjb0031Model.inc');
require_once('knjb0031Query.inc');

class knjb0031Controller extends Controller
{
    public $ModelClassName = "knjb0031Model";
    public $ProgramID      = "KNJB0031";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "search":
                case "edit":
                case "reset":
                    $this->callView("knjb0031Form1");
                    break 2;
                case "subform1": //受講クラス
                    $this->callView("knjb0031SubForm1");
                    break 2;
                case "subform2": //科目担任
                    $this->callView("knjb0031SubForm2");
                    break 2;
                case "subform3": //使用施設
                    $this->callView("knjb0031SubForm3");
                    break 2;
                case "subform4": //教科書
                    $this->callView("knjb0031SubForm4");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":  //削除
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csv2": // 自動生成
                    if (!$sessionInstance->getDownloadModel2()) {
                        $this->callView("knjb0031Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0031Ctl = new knjb0031Controller();
//var_dump($_REQUEST);
