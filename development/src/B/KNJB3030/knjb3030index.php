<?php

require_once('for_php7.php');

require_once('knjb3030Model.inc');
require_once('knjb3030Query.inc');

class knjb3030Controller extends Controller
{
    public $ModelClassName = "knjb3030Model";
    public $ProgramID      = "KNJB3030";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "group":
                case "edit":
                case "reset":
                    $this->callView("knjb3030Form2");
                    break 2;
                case "hrSearch": //HRクラス
                    $this->callView("knjb3030SubFormHr");
                    break 2;
                case "lcSearch": //授業クラス
                    $this->callView("knjb3030SubFormLc");
                    break 2;
                case "subformStaff": //科目担任
                    $this->callView("knjb3030SubFormStaff");
                    break 2;
                case "subformFacility": //使用施設
                    $this->callView("knjb3030SubFormFacility");
                    break 2;
                case "subformSikenKaizyou": //試験会場
                    $this->callView("knjb3030SubFormSikenKaizyou");
                    break 2;
                case "subformTextBook": //教科書
                    $this->callView("knjb3030SubFormTextBook");
                    break 2;
                case "insert":  //追加
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
                    $this->callView("knjb3030Form1");
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
                    $args["left_src"] = "knjb3030index.php?cmd=list";
                    $args["right_src"] = "knjb3030index.php?cmd=edit";
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
$knjb3030Ctl = new knjb3030Controller();
//var_dump($_REQUEST);
