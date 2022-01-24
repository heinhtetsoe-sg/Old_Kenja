<?php

require_once('for_php7.php');

require_once('knjd410mModel.inc');
require_once('knjd410mQuery.inc');

class knjd410mController extends Controller
{
    public $ModelClassName = "knjd410mModel";
    public $ProgramID      = "KNJD410M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            $sessionInstance->knjd410mModel();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "combo":
                    $this->callView("knjd410mForm1");
                    break 2;
                case "edit":
                case "edit2":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $this->ProgramID);
                    $this->callView("knjd410mForm2");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $this->ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("combo");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $this->ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjd410mindex.php?cmd=list";
                    $args["right_src"] = "knjd410mindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjd410mCtl = new knjd410mController();
