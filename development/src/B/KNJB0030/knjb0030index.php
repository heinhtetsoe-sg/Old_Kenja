<?php
require_once('knjb0030Model.inc');
require_once('knjb0030Query.inc');

class knjb0030Controller extends Controller
{
    public $ModelClassName = "knjb0030Model";
    public $ProgramID      = "KNJB0030";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "group":
                case "edit":
                case "reset":
                    $this->callView("knjb0030Form2");
                    break 2;
                case "subform1": //受講クラス
                    $this->callView("knjb0030SubForm1");
                    break 2;
                case "subform2": //科目担任
                    $this->callView("knjb0030SubForm2");
                    break 2;
                case "subform3": //使用施設
                    $this->callView("knjb0030SubForm3");
                    break 2;
                case "subform4": //教科書
                    $this->callView("knjb0030SubForm4");
                    break 2;
                case "insert":  //追加---2004.04.22
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":  //コピー
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "sort":
                case "list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjb0030Form1");
                    break 2;
                case "delete":  //削除
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjb0030index.php?cmd=list";
                    $args["right_src"] = "knjb0030index.php?cmd=edit";
                    $args["cols"] = "52%,48%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0030Ctl = new knjb0030Controller();
//var_dump($_REQUEST);
