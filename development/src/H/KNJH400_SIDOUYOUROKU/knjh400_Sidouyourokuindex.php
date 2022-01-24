<?php

require_once('for_php7.php');

require_once('knjh400_SidouyourokuModel.inc');
require_once('knjh400_SidouyourokuQuery.inc');

class knjh400_SidouyourokuController extends Controller
{
    public $ModelClassName = "knjh400_SidouyourokuModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi3":
                case "torikomi4":
                case "reload":
                case "reload2":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjh400_SidouyourokuForm1");
                    break 2;
                case "subform1":    //通知表所見参照
                    $this->callView("knjh400_SidouyourokuSubForm1");
                    break 2;
                case "subform4":    //成績
                    $this->callView("knjh400_SidouyourokuSubForm4");
                    break 2;
                case "tyousasyoSelect":    //調査書内容選択
                    $this->callView("knjh400_SidouyourokuSubTyousasyoSelect");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    $this->callView("knjh400_SidouyourokuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_SidouyourokuCtl = new knjh400_SidouyourokuController();
